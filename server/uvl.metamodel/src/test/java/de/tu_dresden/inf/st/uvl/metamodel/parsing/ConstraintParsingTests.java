package de.tu_dresden.inf.st.uvl.metamodel.parsing;

import de.tu_dresden.inf.st.uvl.metamodel.exception.ParseError;
import de.tu_dresden.inf.st.uvl.metamodel.main.UVLModelFactory;
import de.tu_dresden.inf.st.uvl.metamodel.main.ModelType;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ExpressionConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintParsingTests {

	private static final String TEST_RESOURCES_PREFIX = "src" + File.separator + "test" + File.separator + "resources" + File.separator;
	private static final String ARITHMETIC_MODEL_PREFIX = TEST_RESOURCES_PREFIX + "arithmetic_level" + File.separator;
	private static final String ARITHMETIC_SIMPLE_CONSTRAINTS = ARITHMETIC_MODEL_PREFIX + "arithmetic-simpleconstraints.uvl";
	private static final String BP_MODEL_PREFIX = TEST_RESOURCES_PREFIX + "bp" + File.separator;
	private static final String WATER_TANK = BP_MODEL_PREFIX + "water_tank.uvl";

	private UVLModelFactory factory;
	private FeatureModel baseFeatureModel;
	private FeatureModel bpFeatureModel;

	@BeforeEach
	void setUp() {
		factory = new UVLModelFactory();
		// Parse the feature model first so constraints can reference actual features and attributes
		baseFeatureModel = factory.parse(Paths.get(ARITHMETIC_SIMPLE_CONSTRAINTS));
		assertNotNull(baseFeatureModel, "Feature model should be parsed successfully");
		
		// Parse BP feature model
		bpFeatureModel = factory.parse(Paths.get(WATER_TANK), ModelType.BP);
		assertNotNull(bpFeatureModel, "BP Feature model should be parsed successfully");
	}

	// ======================== BASE (Arithmetic) Constraint Tests ========================

	@Test
	void parseSimpleAdditionConstraintWithValidContext() {
		String expectedConstraintString = "B.Price + C.Price < 10";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, baseFeatureModel);
		assertNotNull(parsedConstraint, "Parsed constraint should not be null");
        assertInstanceOf(ExpressionConstraint.class, parsedConstraint, "Constraint should be an ExpressionConstraint");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseMultiplicationConstraintWithValidContext() {
		String expectedConstraintString = "B.Price * B.Fun > 20";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, baseFeatureModel);
		assertNotNull(parsedConstraint, "Parsed constraint should not be null");
		assertInstanceOf(ExpressionConstraint.class, parsedConstraint, "Constraint should be an ExpressionConstraint");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseComplexArithmeticConstraintWithValidContext() {
		String expectedConstraintString = "B.Price + C.Price * B.Fun >= 50";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, baseFeatureModel);
		assertNotNull(parsedConstraint, "Parsed complex arithmetic constraint should not be null");
		assertInstanceOf(ExpressionConstraint.class, parsedConstraint, "Constraint should be an ExpressionConstraint");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseMalformedConstraintThrows() {
		assertThrows(ParseError.class, () -> factory.parseConstraint("B.Price + < 10", baseFeatureModel),
				"Malformed constraint should throw ParseError");
	}

	// ======================== BP Constraint Tests ========================

	@Test
	void parseBPRequestedConstraint() {
		String expectedConstraintString = "requested(HOT)";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, bpFeatureModel, ModelType.BP);
		assertNotNull(parsedConstraint, "Parsed BP requested constraint should not be null");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseBPConflictingConstraint() {
		String expectedConstraintString = "conflicting(HOT, COLD, DRAIN, FINISHED)";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, bpFeatureModel, ModelType.BP);
		assertNotNull(parsedConstraint, "Parsed BP conflicting constraint should not be null");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseBPSelectedConstraint() {
		String expectedConstraintString = "selected(DRAIN)";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, bpFeatureModel, ModelType.BP);
		assertNotNull(parsedConstraint, "Parsed BP selected constraint should not be null");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseBPConstraintWithCondition() {
		String expectedConstraintString = "(Env.temp < Config.target_temp) => requested(HOT)";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, bpFeatureModel, ModelType.BP);
		assertNotNull(parsedConstraint, "Parsed BP implication constraint should not be null");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

	@Test
	void parseBPConstraintWithEquivalence() {
		String expectedConstraintString = "(Env.level > Config.target_level) <=> selected(DRAIN)";
		
		Constraint parsedConstraint = factory.parseConstraint(expectedConstraintString, bpFeatureModel, ModelType.BP);
		assertNotNull(parsedConstraint, "Parsed BP equivalence constraint should not be null");

		String actualConstraintString = parsedConstraint.toString();
		assertNotNull(actualConstraintString, "Constraint string representation should not be null");
		assertEquals(expectedConstraintString, actualConstraintString, "Constraint string representation should be equal");
	}

}
