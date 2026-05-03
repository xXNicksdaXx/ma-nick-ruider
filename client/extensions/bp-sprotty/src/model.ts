/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import {
    DefaultTypes,
    GCompartment,
} from '@eclipse-glsp/client';
import { FeatureNode } from "uvl-sprotty";

export class BThreadNode extends FeatureNode {
    get eventContainer(): GCompartment {
        return <GCompartment>(
            this.children.find(element => element.type === DefaultTypes.COMPARTMENT && element.id.endsWith('_event_compartment'))
        );
    }

    hasEvents(): boolean {
        const container = this.eventContainer;
        if (container) {
            return container.children.length !== 0;
        }
        return false;
    }
}
