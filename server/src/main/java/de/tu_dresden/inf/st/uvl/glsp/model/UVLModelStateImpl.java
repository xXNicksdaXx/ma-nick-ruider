/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.vill.model.FeatureModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.server.internal.gmodel.commandstack.GModelCommandStack;
import org.eclipse.glsp.server.model.DefaultGModelState;
import org.eclipse.glsp.server.session.ClientSession;
import org.eclipse.glsp.server.session.ClientSessionListener;
import org.eclipse.glsp.server.session.ClientSessionManager;

@Singleton
public class UVLModelStateImpl extends DefaultGModelState implements UVLModelState, ClientSessionListener {

    protected static Logger LOGGER = LogManager.getLogger(UVLModelStateImpl.class.getSimpleName());

    @Inject
    protected ClientSessionManager clientSessionManager;

    protected FeatureModel featureModel;

    @Override
    @Inject
    public void init() {
        this.clientSessionManager.addListener(this, this.clientId);
        setCommandStack(new GModelCommandStack());
        LOGGER.trace("Created UVLModelState for client {}", this.clientId);
    }

    @Override
    public UVLModelIndex getIndex() {
        return (UVLModelIndex) super.getIndex();
    }

    @Override
    public void updateRoot(final GModelRoot newRoot) {
        setRoot(newRoot);
        this.index = getOrUpdateIndex(newRoot);
        if (this.featureModel != null) {
            getIndex().indexFeatureModel(this.featureModel);
        }
    }

    @Override
    protected UVLModelIndex getOrUpdateIndex(final GModelRoot newRoot) {
        return UVLModelIndex.getOrCreate(getRoot());
    }

    @Override
    public FeatureModel getFeatureModel() {
        return this.featureModel;
    }

    @Override
    public void setFeatureModel(FeatureModel model) {
        this.featureModel = model;
    }

    @Override
    public void sessionDisposed(final ClientSession clientSession) {
        this.index.clear();
    }
}
