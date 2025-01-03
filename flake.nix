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
      buildToolsVersion = "35.0.0";
      androidComposition = pkgs.androidenv.composeAndroidPackages {
        useGoogleAPIs = true;
        useGoogleTVAddOns = true;
        includeSystemImages = true;
        buildToolsVersions = [ buildToolsVersion ];
        platformVersions = [
          "34"
          "35"
        ];
        includeSources = true;
        includeEmulator = true;
      };
    in
    {
      devShells.x86_64-linux.default = pkgs.mkShell rec {
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

          jdk
          gradle
          kotlin

          ktlint
          jdt-language-server
          kotlin-language-server
          androidComposition.androidsdk
        ];
        ANDROID_HOME = "${androidComposition.androidsdk}/libexec/android-sdk";
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${ANDROID_HOME}/build-tools/${buildToolsVersion}/aapt2";
      };
    };
}
