/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import {
    DefaultTypes,
    Deletable,
    deletableFeature,
    EditableLabel,
    editLabelFeature,
    GCompartment,
    GLabel,
    GModelElement,
    Nameable,
    nameFeature,
    RectangularNode,
    Selectable,
    selectFeature,
    WithEditableLabel,
    withEditLabelFeature
} from '@eclipse-glsp/client';

export class LabeledNode extends RectangularNode implements WithEditableLabel, Nameable, Deletable {

    get editableLabel(): (EditableLabel & GModelElement) | undefined {
        const headerComp = this.children.find(element => element.type === DefaultTypes.COMPARTMENT_HEADER);
        if (headerComp) {
            const label = headerComp.children.find(element => element.type === DefaultTypes.LABEL);
            if (label && label instanceof GLabel) {
                return label;
            }
        }
        return undefined;
    }

    get name(): string {
        if (this.editableLabel) {
            return this.editableLabel.text;
        }
        return this.id;
    }

    get headerContainer(): GCompartment {
        return <GCompartment>(
            this.children.find(element => element.type === DefaultTypes.COMPARTMENT_HEADER && element.id.endsWith('_header'))
        );
    }

    override hasFeature(feature: symbol): boolean {
        return super.hasFeature(feature) || feature === nameFeature || feature === withEditLabelFeature;
    }
}

export class FeatureNode extends LabeledNode {
    get attributeContainer(): GCompartment {
        return <GCompartment>(
            this.children.find(element => element.type === DefaultTypes.COMPARTMENT && element.id.endsWith('_attribute_compartment'))
        );
    }

    hasAttributes(): boolean {
        const container = this.attributeContainer;
        if (container) {
            return container.children.length !== 0;
        }
        return false;
    }
}

export class ConstraintBoxNode extends LabeledNode {
    get constraintContainer(): GCompartment {
        return <GCompartment>(
            this.children.find(element => element.type === DefaultTypes.COMPARTMENT && element.id === 'constraint_box_compartment')
        );
    }

    hasConstraints(): boolean {
        const container = this.constraintContainer;
        if (container) {
            return container.children.length !== 0;
        }
        return false;
    }
}

export class EditableGLabel extends GLabel implements EditableLabel {
    override hasFeature(feature: symbol): boolean {
        return super.hasFeature(feature) || feature === editLabelFeature;
    }
}

export class EditableGCompartment extends GCompartment implements Selectable, Deletable {
    selected = false;

    override hasFeature(feature: symbol): boolean {
        return super.hasFeature(feature) || feature === selectFeature || feature === deletableFeature;
    }
}
