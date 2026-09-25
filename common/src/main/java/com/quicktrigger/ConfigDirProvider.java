package com.quicktrigger;

import java.nio.file.Path;

//each loader passes its own config dir in (FabricLoader / FMLPaths)
public interface ConfigDirProvider {
    Path getConfigDir();
}
