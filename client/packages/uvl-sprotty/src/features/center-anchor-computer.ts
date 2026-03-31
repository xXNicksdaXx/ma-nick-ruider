/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import { injectable } from 'inversify';
import { GConnectableElement, IAnchorComputer, Point, PolylineEdgeRouter } from "@eclipse-glsp/client";

export const CENTERED_ANCHOR_KIND = 'centered';

/**
 * This CenteredAnchor computes a custom anchor point for the features.
 * It returns a point at the horizontal center of the element, and either at the top or bottom border depending on whether the reference point is above or below the element.
 * This allows edges to connect to the center of the top or bottom border of the element.
 */
@injectable()
export class CenteredAnchor implements IAnchorComputer {

    static KIND = PolylineEdgeRouter.KIND + ':' + CENTERED_ANCHOR_KIND;

    get kind(): string {
        return CenteredAnchor.KIND;
    }

    getAnchor(connectable: GConnectableElement, refPoint: Point): Point {
        const centerX = connectable.bounds.x + connectable.bounds.width / 2;
        const centerY = connectable.bounds.y + connectable.bounds.height / 2;
        const topY = connectable.bounds.y;
        const bottomY = connectable.bounds.y + connectable.bounds.height;

        // determine whether the reference point is above or below the element
        if (refPoint.y < centerY) {
            return { x: centerX, y: topY };
        } else {
            return { x: centerX, y: bottomY };
        }
    }
}
