{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-23.11";

    utils.url = "github:numtide/flake-utils";
  };

  outputs = { nixpkgs, ... }@inputs: inputs.utils.lib.eachSystem [
    "x86_64-linux"
    "aarch64-linux"
    "aarch64-darwin"
  ]
    (system:
      let
        pkgs = import nixpkgs {
          inherit system;
        };
      in
      {
        devShells.default = pkgs.mkShell rec {
          name = "datastructure";

          packages = with pkgs; [
            temurin-bin
            temurin-jre-bin
            gradle
          ];

          shellHook = ''
            export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:${
              with pkgs;
              lib.makeLibraryPath [ 
                libGL 
                xorg.libX11 
                xorg.libXi 
                xorg.libXxf86vm 
                xorg.libXtst 
              ]
            }"
          '';
        };
      });
}

