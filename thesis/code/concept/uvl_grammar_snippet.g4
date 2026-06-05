featureModel:
	namespace? NEWLINE? includes? NEWLINE? imports? NEWLINE? features? NEWLINE? constraints? EOF;
...
features: FEATURES_KEY NEWLINE INDENT feature DEDENT;