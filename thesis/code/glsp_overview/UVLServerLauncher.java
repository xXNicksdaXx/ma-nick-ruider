public static void main(final String[] args) {
    new UVLServerLauncher().launch(args);
}

protected ServerModule createServerModule() {
    return new ServerModule().configureDiagramModule(createDiagramModule());
}

protected DiagramModule createDiagramModule() {
    return new UVLDiagramModule();
}