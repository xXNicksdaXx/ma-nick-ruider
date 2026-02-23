/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import { angleOfPoint, GEdge, GEdgeView, IViewArgs, Point, RectangularNodeView, RenderingContext, toDegrees } from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';
import { svg } from 'sprotty';

import { LabeledNode } from "./model";

// Workaround for typing issues: Creating elements returns a `JSX.Element` instead of a `VNode`, which causes compilation errors.
// Therefore, type the VNode elements as `any` to avoid these problems.
// See also: https://github.com/eclipse/sprotty/issues/178
// The solutions described there did not work in this case.
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const JSX = { createElement: svg };

@injectable()
export class FeatureNodeView extends RectangularNodeView {

    override render(node: LabeledNode, context: RenderingContext, args?: IViewArgs): VNode | undefined {
        if (!this.isVisible(node, context)) {
            return undefined;
        }

        return (
            <g>
                <rect
                    x={0}
                    y={0}
                    width={Math.max(0, node.bounds.width)}
                    height={Math.max(0, node.bounds.height)}
                    class-sprotty-node={true}
                    class-selected={node.selected}
                    class-mouseover={node.hoverFeedback}
                />

                {this.renderSeparatorLine(node)}

                {context.renderChildren(node)}
            </g>
        );
    }

    protected renderSeparatorLine(node: LabeledNode): VNode {
        return (
            <line
                x1={0}
                y1={Math.max(0, node.headerContainer.bounds.height)}
                x2={Math.max(0, node.bounds.width)}
                y2={Math.max(0, node.headerContainer.bounds.height)}
                class-separator-line={true}
            />
        );
    }
}

@injectable()
export class CircleEdgeView extends GEdgeView {
    protected OFFSET: number = 2;
    protected RADIUS: number = 4;

    protected override renderAdditionals(edge: GEdge, segments: Point[], context: RenderingContext): VNode[] {
        const additionals = super.renderAdditionals(edge, segments, context);
        const p1 = segments[segments.length - 2];
        const p2 = segments[segments.length - 1];

        const dx = p2.x - p1.x;
        const dy = p2.y - p1.y;
        const len = Math.sqrt(dx * dx + dy * dy) || 1;
        const nx = dx / len;
        const ny = dy / len;

        const cx = p2.x - nx * this.OFFSET;
        const cy = p2.y - ny * this.OFFSET;

        const circle = (
            <circle
                class-sprotty-edge={true}
                class-circle={true}
                cx={cx}
                cy={cy}
                r={this.RADIUS}
            />
        );

        additionals.push(circle);
        return additionals;
    }
}

@injectable()
export class SectorEdgeView extends GEdgeView {
    protected OFFSET: number = 2;
    protected RADIUS: number = 16;

    protected override renderAdditionals(edge: GEdge, segments: Point[], context: RenderingContext): VNode[] {
        const additionals = super.renderAdditionals(edge, segments, context);
        const neighbors: GEdge[] = this.getNeighborhood(edge);

        for (const neighbor of neighbors) {
            const source = segments[0];
            const originalTarget = segments[1];

            const neighborSegments = this.edgeRouterRegistry.route(neighbor)
            const neighborTarget = neighborSegments[1];

            let s1 = this.getSectorPoint(source, originalTarget, this.RADIUS);
            let s2 = this.getSectorPoint(source, neighborTarget, this.RADIUS);

            if (this.isToTheRight(source, s1, s2)) {
                continue;
            }

            const largeArcFlag = this.calculateLargeArcFlag(s1, s2, source);

            const sector = (
                <path
                    class-sprotty-edge={true}
                    class-sector={true}
                    d={`
                        M ${s1.x} ${s1.y} A ${this.RADIUS} ${this.RADIUS} 0 ${largeArcFlag} 1 ${s2.x} ${s2.y} 
                        M ${source.x} ${source.y} L ${s1.x} ${s1.y} L ${s2.x} ${s2.y} Z
                    `}
                />
            );
            additionals.push(sector);
        }

        return additionals;
    }

    protected getNeighborhood(edge: GEdge): GEdge[] {
        const parent = edge.parent;
        if (!parent) {
            return [];
        }

        return parent.children
            .filter(element => element instanceof GEdge && element.type === edge.type)
            .map(element => element as GEdge)
            .filter(e => e.source === edge.source);
    }

    protected getSectorPoint(source: Point, target: Point, radius: number): Point {
        const dx = target.x - source.x;
        const dy = target.y - source.y;
        const len = Math.sqrt(dx * dx + dy * dy) || 1;
        return {
            x: source.x + (dx / len) * radius,
            y: source.y + (dy / len) * radius
        };
    }

    protected isToTheRight(source: Point, target: Point, point: Point): boolean {
        const cross = (target.x - source.x) * (point.y - source.y) - (target.y - source.y) * (point.x - source.x);
        return cross < 0;
    }

    protected calculateLargeArcFlag(s1: Point, s2: Point, source: Point): number {
        const dx1 = s1.x - source.x;
        const dy1 = s1.y - source.y;
        const dx2 = s2.x - source.x;
        const dy2 = s2.y - source.y;
        const dot = (dx1 * dx2 + dy1 * dy2) / (this.RADIUS * this.RADIUS);
        const clamped = Math.max(-1, Math.min(1, dot));
        const angle = Math.acos(clamped);
        return angle > Math.PI ? 1 : 0;
    }
}

@injectable()
export abstract class TrimmedGEdgeView extends GEdgeView {
    protected abstract readonly shortenDistance: number; // distance to trim at both ends
    protected abstract readonly trimFirstSegment: boolean; // whether to trim the first segment

    protected override renderLine(_edge: GEdge, segments: Point[], _context: RenderingContext): VNode {
        if (this.shortenDistance <= 0 || segments.length < 2) {
            // If no shortening is needed or not enough segments, return the original path
            return <path d={this.createPathForSegments(segments)} />;
        }
        // Trim the endpoints of the segments to shorten the edge
        const trimmedSegments = this.trimSegmentEndpoints(segments, this.shortenDistance);
        return <path d={this.createPathForSegments(trimmedSegments)} />;
    }

    protected trimSegmentEndpoints(segments: Point[], offset: number): Point[] {
        if (segments.length < 2) {
            return segments;
        }

        const trimmed: Point[] = [...segments];

        // shorten the first segment
        if (this.trimFirstSegment) {
            const startDir = Point.normalize(Point.subtract(segments[1], segments[0]));
            trimmed[0] = Point.add(segments[0], { x: startDir.x * offset, y: startDir.y * offset });
        }

        // shorten the last segment
        const endDir = Point.normalize(Point.subtract(segments[segments.length - 2], segments[segments.length - 1]));
        trimmed[trimmed.length - 1] = Point.add(segments[segments.length - 1], { x: endDir.x * offset, y: endDir.y * offset });

        return trimmed;
    }
}

@injectable()
export class SingleArrowEdgeView extends TrimmedGEdgeView {
    protected override shortenDistance = 2;
    protected override trimFirstSegment = false;

    protected override renderAdditionals(edge: GEdge, segments: Point[], context: RenderingContext): VNode[] {
        const additionals = super.renderAdditionals(edge, segments, context);
        const p1 = segments[segments.length - 2];
        const p2 = segments[segments.length - 1];

        const arrowPath = 'M 0,0 L 6,-3 M 0,0 L 6,3'; // line-based arrow
        const offset = 1; // offset to move the arrow away from the target

        const arrow = (
            <path
                class-sprotty-edge={true}
                class-triangle={true}
                d={arrowPath}
                transform={`rotate(${toDegrees(angleOfPoint(Point.subtract(p1, p2)))} ${p2.x} ${p2.y}) 
                              translate(${p2.x} ${p2.y})
                              translate(${offset}, 0)`}
            />
        );
        additionals.push(arrow);
        return additionals;
    }
}

@injectable()
export class DoubleArrowEdgeView extends TrimmedGEdgeView {
    protected override shortenDistance = 2;
    protected override trimFirstSegment = true;

    protected override renderAdditionals(edge: GEdge, segments: Point[], context: RenderingContext): VNode[] {
        const additionals = super.renderAdditionals(edge, segments, context);
        const source1 = segments[0];
        const source2 = segments[1];
        const target1 = segments[segments.length - 2];
        const target2 = segments[segments.length - 1];

        const arrowPath = 'M 0,0 L 6,-3 M 0,0 L 6,3'; // line-based arrow
        const offset = 1; // offset to move the arrow away from the target

        const sourceArrow = (
            <path
                class-sprotty-edge={true}
                class-triangle={true}
                d={arrowPath}
                transform={`rotate(${toDegrees(angleOfPoint(Point.subtract(source2, source1)))} ${source1.x} ${source1.y}) 
                              translate(${source1.x} ${source1.y})
                              translate(${offset}, 0)`}
            />
        );
        const targetArrow = (
            <path
                class-sprotty-edge={true}
                class-triangle={true}
                d={arrowPath}
                transform={`rotate(${toDegrees(angleOfPoint(Point.subtract(target1, target2)))} ${target2.x} ${target2.y}) 
                              translate(${target2.x} ${target2.y})
                              translate(${offset}, 0)`}
            />
        );
        additionals.push(sourceArrow);
        additionals.push(targetArrow);
        return additionals;
    }
}