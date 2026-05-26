const registry = { 'uvl-default':{configure:configureUVL}, 'uvl-bp-sse':{configure:configureSSE} };
for (const id of __UVL_COMMAND_CONTRIBUTION_IDS__) registry[id].configure(context);