class UvlDiagramStarter extends GLSPStarter {
    createContainer(...containerConfiguration: ContainerConfiguration): Container {
        const pluginModules = resolveWebviewPluginModules();
        return initializeUvlDiagramContainer(
            new Container(), ...pluginModules, ...containerConfiguration);
    }
}

export function launch(): void {
     new UvlDiagramStarter();
}