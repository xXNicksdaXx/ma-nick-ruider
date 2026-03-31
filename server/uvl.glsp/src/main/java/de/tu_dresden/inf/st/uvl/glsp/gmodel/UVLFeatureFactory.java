/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.generic.AbstractSingleGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GCompartment;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GLayoutOptions;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GConstants;

public class UVLFeatureFactory extends AbstractSingleGModelFactory<Feature, GNode> {

  @Inject protected UVLAttributeFactory attributeFactory;

  @Override
  protected GNode create(final Feature feature) {
    return createFeature(feature);
  }

  protected GNode createFeature(final Feature feature) {
    UVLModelIndex index = modelState.getIndex();
    String id =
        index
            .getIdFor(feature)
            .orElseThrow(
                () ->
                    new IllegalStateException("Feature not indexed: " + feature.getFeatureName()));

    GNodeBuilder nodeBuilder =
        new GNodeBuilder(UVLModelTypes.FEATURE)
            .id(id)
            .layout(GConstants.Layout.VBOX)
            .layoutOptions(
                new GLayoutOptions()
                    .paddingTop(0)
                    .paddingLeft(0)
                    .paddingRight(0)
                    .paddingBottom(0.0)
                    .resizeContainer(true))
            .add(buildHeader(id, feature.getFeatureName(), feature.getCardinality()))
            .add(buildAttributeCompartment(id, feature));

    applyNodeData(nodeBuilder, id);
    return nodeBuilder.build();
  }

  protected GCompartment buildHeader(
      final String id, final String name, final Cardinality cardinality) {
    GLabel headerLabel =
        new GLabelBuilder(UVLModelTypes.FEATURE_NAME)
            .id(id + "_header_label")
            .addCssClass("bold-title")
            .text(name)
            .build();
    GCompartmentBuilder headerBuilder =
        new GCompartmentBuilder(DefaultTypes.COMPARTMENT_HEADER)
            .id(id + "_header")
            .layout(GConstants.Layout.HBOX)
            .layoutOptions(
                new GLayoutOptions()
                    .paddingTop(4)
                    .paddingLeft(4)
                    .paddingRight(4)
                    .paddingBottom(4.0)
                    .hAlign(GConstants.HAlign.CENTER)
                    .resizeContainer(true))
            .add(headerLabel);

    if (cardinality != null) {
      addCardinality(cardinality, id, headerBuilder);
    }

    return headerBuilder.build();
  }

  protected void addCardinality(
      final Cardinality cardinality, final String id, GCompartmentBuilder compartmentBuilder) {
    GLabel cardinalityLabel =
        new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL)
            .id(id + "_cardinality_label")
            .text(FeatureModelUtil.getCardinalityText(cardinality))
            .build();
    compartmentBuilder
        .add(new GLabelBuilder(DefaultTypes.LABEL).text(" [").build())
        .add(cardinalityLabel)
        .add(new GLabelBuilder(DefaultTypes.LABEL).text("]").build());
  }

  protected GCompartment buildAttributeCompartment(final String id, final Feature feature) {
    GCompartmentBuilder compartmentBuilder =
        new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
            .id(id + "_attribute_compartment")
            .layout(GConstants.Layout.VBOX)
            .layoutOptions(
                new GLayoutOptions()
                    .paddingTop(0)
                    .paddingLeft(0)
                    .paddingRight(0)
                    .paddingBottom(0.0)
                    .hAlign(GConstants.HAlign.LEFT)
                    .resizeContainer(true));

    feature.getAttributes().values().stream()
        .map(attributeFactory::create)
        .forEach(compartmentBuilder::add);

    return compartmentBuilder.build();
  }
}
