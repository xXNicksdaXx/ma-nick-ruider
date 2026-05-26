new webpack.DefinePlugin({
    __UVL_BUILD_PROFILE_ID__: JSON.stringify(buildProfile.id),
    __UVL_CONTAINER_MODULE_IDS__: JSON.stringify(buildProfile.containerModuleIds||[])
}),
new CopyPlugin({ patterns:[{ from: buildProfile.serverJarAbsolutePath, to: 'dist/uvl-server.glsp.jar' }] })