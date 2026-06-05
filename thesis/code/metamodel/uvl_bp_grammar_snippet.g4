features: FEATURES_KEY NEWLINE INDENT feature envConfigFeature? envConfigFeature? DEDENT;

envConfigFeature:
    reference attributes? NEWLINE;

constraint:
    ...
    | bpConstraint          # BPEventConstraint
    | ...;

bpConstraint:
    requestedConstraint
    | blockedConstraint
    | waitedForConstraint
    | selectedConstraint
    | conflictingConstraint;

conflictingConstraint:
    CONFLICTING_KEY OPEN_PAREN (reference COMMA)* reference CLOSE_PAREN;