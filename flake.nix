{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs =
    {
      self,
      nixpkgs,
    }:
    let
      pkgs = import nixpkgs {
        system = "x86_64-linux";
        config.allowUnfree = true;
        config.android_sdk.accept_license = true;
      };
    in
    {
      devShells.x86_64-linux.default = pkgs.mkShell {
        packages = with pkgs; [
          # (android-studio.withSdk
          #   (androidenv.composeAndroidPackages {
          #     useGoogleAPIs = true;
          #     useGoogleTVAddOns = true;
          #     includeSystemImages = true;
          #     platformVersions = [
          #       "34"
          #       "35"
          #     ];
          #     includeSources = true;
          #     includeEmulator = true;
          #   }).androidsdk
          # )
          android-studio
        ];
      };
    };
}
