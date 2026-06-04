/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.model;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLGModelFactory;
import de.tu_dresden.inf.st.uvl.metamodel.exception.ParseErrorList;
import de.tu_dresden.inf.st.uvl.metamodel.main.UVLModelFactory;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GraphFactory;
import org.eclipse.glsp.server.actions.SaveModelAction;
import org.eclipse.glsp.server.features.core.model.RequestModelAction;
import org.eclipse.glsp.server.features.core.model.SourceModelStorage;
import org.eclipse.glsp.server.gmodel.GModelStorage;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.types.GLSPServerException;
import org.eclipse.glsp.server.utils.ClientOptionsUtil;

public class UVLSourceModelStorage extends GModelStorage implements SourceModelStorage {

  protected static Logger LOGGER =
      LogManager.getLogger(UVLSourceModelStorage.class.getSimpleName());

  @Inject protected UVLModelState modelState;

  @Inject protected UVLGModelFactory gModelFactory;

  @Override
  public void loadSourceModel(RequestModelAction action) {
    File featureModelFile = convertToFile(action.getOptions());
    validateModelPath(featureModelFile, ClientOptionsUtil.getSourceUri(action.getOptions()));
    loadFeatureModel(featureModelFile);

    String filePath = featureModelFile.getAbsolutePath();
    File notationFile = getNotationFile(filePath);
    loadGModel(notationFile);
  }

  protected void loadFeatureModel(final File featureModelFile) {
    String filePath = featureModelFile.getAbsolutePath();

    try {
      FeatureModel featureModel = parseFeatureModel(Paths.get(filePath));
      modelState.setFeatureModel(featureModel);
    } catch (IndexOutOfBoundsException e) {
      modelState.setFeatureModel(new FeatureModel());
    } catch (ParseErrorList e) {
      String errorList =
          e.getErrorList().stream()
              .reduce("", (acc, error) -> acc + error.toString() + "\n", String::concat);
      LOGGER.error(errorList);
      throw new GLSPServerException("Check the UVL file for the following error: " + errorList, e);
    } catch (Exception e) {
      LOGGER.error(e);
      throw new GLSPServerException(
          "An unexpected error occurred during loading of the FeatureModel file: "
              + featureModelFile.toURI(),
          e);
    }
  }

  protected FeatureModel parseFeatureModel(final Path path) {
    try {
      String content = Files.readString(path);
      boolean isEmpty = content.trim().isEmpty();
      if (isEmpty) {
        return new FeatureModel();
      }
    } catch (IOException e) {
      LOGGER.error(e);
      throw new GLSPServerException("Could not load FeatureModel from file: " + path.toUri(), e);
    }

    UVLModelFactory uvlModelFactory = new UVLModelFactory();
    return uvlModelFactory.parse(path);
  }

  protected void loadGModel(final File notationFile) {
    try (Reader reader =
        new InputStreamReader(new FileInputStream(notationFile), StandardCharsets.UTF_8)) {
      GGraph root = gson.fromJson(reader, GGraph.class);
      if (root == null) {
        boolean isEmpty = notationFile.length() == 0;
        if (isEmpty) {
          GModelRoot newRoot = createNewEmptyRoot(modelState);
          modelState.updateRoot(newRoot);
          gModelFactory.createGModel();
          saveGModel(notationFile);
          return;
        }
        throw new IOException(
            "Could not deserialize GModel file contents of: " + notationFile.toURI());
      }
      modelState.updateRoot(root);
    } catch (IOException e) {
      LOGGER.error(e);
      throw new GLSPServerException("Could not load GModel from file: " + notationFile.toURI(), e);
    }
  }

  @Override
  protected GModelRoot createNewEmptyRoot(final GModelState modelState) {
    GModelRoot root = GraphFactory.eINSTANCE.createGGraph();
    root.setId(ClientOptionsUtil.getSourceUri(modelState.getClientOptions()).orElse("root"));
    root.setRevision(modelState.getRoot() != null ? root.getRevision() : -1);
    root.setType(DefaultTypes.GRAPH);
    return root;
  }

  @Override
  public void saveSourceModel(SaveModelAction action) {
    File featureModelFile = convertToFile(action);
    validateModelPath(
        featureModelFile, ClientOptionsUtil.getSourceUri(modelState.getClientOptions()));
    saveFeatureModel(featureModelFile);

    String filePath = featureModelFile.getAbsolutePath();
    File notationFile = getNotationFile(filePath);
    saveGModel(notationFile);
  }

  protected void saveFeatureModel(final File featureModelFile) {
    final FeatureModel featureModel = modelState.getFeatureModel();
    try (Writer writer =
        new OutputStreamWriter(new FileOutputStream(featureModelFile), StandardCharsets.UTF_8)) {
      String uvlModel = featureModel.toString();
      writer.write(uvlModel);
    } catch (IOException e) {
      LOGGER.error(e);
      throw new GLSPServerException(
          "An error occurred during save process of the FeatureModel file.", e);
    }
  }

  protected void saveGModel(final File notationFile) {
    try (Writer writer =
        new OutputStreamWriter(new FileOutputStream(notationFile), StandardCharsets.UTF_8)) {
      GGraph root = (GGraph) modelState.getRoot();
      gson.toJson(root, GGraph.class, writer);
    } catch (IOException e) {
      LOGGER.error(e);
      throw new GLSPServerException("An error occurred during save process of the GModel file.", e);
    }
  }

  private static File getNotationFile(String featureModelFilePath) {
    String notationFilePath = featureModelFilePath.replaceAll("\\.uvl$", ".notation.json");
    Path path = Paths.get(notationFilePath);
    if (!Files.exists(path)) {
      LOGGER.info("GModel file does not exist: {}. Creating new notation file.", path.toUri());
      try {
        Files.createFile(path);
      } catch (IOException e) {
        LOGGER.error(e);
        throw new GLSPServerException("Could not create new GModel file: " + path.toUri(), e);
      }
    }
    return path.toFile();
  }

  private static void validateModelPath(File featureModelFile, Optional<String> sourceUri) {
    if (sourceUri.isEmpty()) {
      throw new GLSPServerException("Missing source URI for model path validation.");
    }

    Path baseDir = getBaseDirFromSourceUri(sourceUri.get());
    Path candidate = featureModelFile.toPath().toAbsolutePath().normalize();

    if (!candidate.startsWith(baseDir)) {
      throw new GLSPServerException("Rejected model path outside source directory: " + candidate);
    }
  }

  private static Path getBaseDirFromSourceUri(String sourceUri) {
    Path sourcePath = toSourcePath(sourceUri);
    Path parent = sourcePath.getParent();
    if (parent == null) {
      throw new GLSPServerException("Invalid source URI for model path validation: " + sourceUri);
    }
    return parent;
  }

  static Path toSourcePath(String sourceUri) {
    if (sourceUri.startsWith("file:")) {
      return Paths.get(URI.create(sourceUri.replace(" ", "%20"))).toAbsolutePath().normalize();
    }
    return Paths.get(sourceUri).toAbsolutePath().normalize();
  }
}
