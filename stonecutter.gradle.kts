plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.7-fabric"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["mod_id"] = "\"${properties.get<String>("mod.id")}\";"
    swaps["mod_name"] = "\"${properties.get<String>("mod.name")}\";"
    swaps["mod_group"] = "\"${properties.get<String>("mod.group")}\";"
    swaps["minecraft"] = "\"${current.version}\";"
    constants["release"] = true
}
