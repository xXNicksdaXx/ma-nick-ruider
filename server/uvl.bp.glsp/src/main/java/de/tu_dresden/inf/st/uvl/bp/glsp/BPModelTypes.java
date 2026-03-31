/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp;

import org.eclipse.glsp.graph.DefaultTypes;

/**
 * Defines string constants for all element types used in UVL BP diagrams.
 *
 * <p>For corresponding client-side types, see <code>bp-model-types.ts</code>.
 *
 * <p>This class is not intended to be instantiated.
 */
public final class BPModelTypes {
  public static final String BP_ENV = DefaultTypes.NODE + "env";
  public static final String BP_CONFIG = DefaultTypes.NODE + ":config";

  public static final String B_THREAD = DefaultTypes.NODE + ":b-thread";

  public static final String REQUESTED_EVENT = DefaultTypes.COMPARTMENT + ":requested-event";
  public static final String BLOCKED_EVENT = DefaultTypes.COMPARTMENT + ":blocked-event";
  public static final String WAITED_FOR_EVENT = DefaultTypes.COMPARTMENT + ":waited-for-event";
  public static final String EVENT_NAME = DefaultTypes.LABEL + ":eventName";
  public static final String EVENT_PRIORITY = DefaultTypes.LABEL + ":eventPriority";

  public static final String REQUESTED = DefaultTypes.EDGE + ":requested";
  public static final String BLOCKED = DefaultTypes.EDGE + ":blocked";
  public static final String WAITED_FOR = DefaultTypes.EDGE + ":waited-for";
  public static final String SELECTED = DefaultTypes.EDGE + ":selected";
  public static final String CONFLICTING = DefaultTypes.EDGE + ":conflicting";

  /** Private constructor to prevent instantiation of this utility class. */
  private BPModelTypes() {
    // Prevent instantiation
  }
}
