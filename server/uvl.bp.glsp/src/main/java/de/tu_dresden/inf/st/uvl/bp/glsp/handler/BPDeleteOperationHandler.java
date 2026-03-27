/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLDeleteOperationHandler;

public class BPDeleteOperationHandler extends UVLDeleteOperationHandler {

	@Override
	protected boolean isDeletableAttributeElement(final String elementType) {
		return super.isDeletableAttributeElement(elementType)
				|| BPModelTypes.REQUESTED_EVENT.equals(elementType)
				|| BPModelTypes.BLOCKED_EVENT.equals(elementType)
				|| BPModelTypes.WAITED_FOR_EVENT.equals(elementType);
	}
}

