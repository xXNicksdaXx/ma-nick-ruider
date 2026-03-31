/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.launch;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPDiagramModule;
import de.tu_dresden.inf.st.uvl.glsp.launch.UVLServerLauncher;
import org.eclipse.glsp.server.di.DiagramModule;

/** Launcher for the BP extension server that reuses the base UVL server bootstrapping. */
public class BPServerLauncher extends UVLServerLauncher {

  public static void main(final String[] args) {
    new BPServerLauncher().launch(args);
  }

  @Override
  protected String getProcessName() {
    return "UVLBPGlspServer";
  }

  @Override
  protected DiagramModule createDiagramModule() {
    return new BPDiagramModule();
  }
}
