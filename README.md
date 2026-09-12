# <img src="https://github.com/RaydanOMGr/DirectTouch/blob/master/src/main/resources/assets/directtouch/icon.png?raw=true&amp;" width="128"/><br>DirectTouch
A fast, reliable and launcher-independent proxy implementation for the [Touch Controller](https://modrinth.com/mod/touchcontroller) mod
![](https://github.com/RaydanOMGr/RaydanOMGr.github.io/blob/main/screenshots/directtouch/screenshot_game.webp?raw=true)
<small>TouchController on PojavLauncher using DirectTouch</small>

## What launchers does this work on?
The mod has been tested on and is guaranteed to work on:
- [PojavLauncher](https://github.com/pojavlauncherteam/pojavlauncher/)
- [Amethyst](https://github.com/AngelAuraMC/Amethyst-Android)
- [MojoLauncher](https://github.com/MojoLauncher/MojoLauncher)

## Features
The DirectTouch mod is made for seamless integration into the gameplay process.

Due to that, it does not provide any features or configuration of its own: it simply makes TouchController work,
and it does this in an optimal way, that is faster, more reliable and does not require a launcher to support the mod!

## Usage
- Download the jar for your version of TouchController
  - If you are using TouchController 0.3.1-alpha (1.21.1 or newer) then download `directtouch-<X.Y.Z>-tc0.3.1-alpha<W>-<loader>.jar`
  - If you are using TouchController 0.2.1-beta (1.21 or older) then download `directtouch-<X.Y.Z>-tc0.2.1-beta<W>-<loader>.jar`
  - X.Y.Z corresponds to the version of DirectTouch, W is the release of TouchController (e.g. 0.3.1-alpha03 ← this is W) and loader is your modloader (e.g. fabric) 
- Put it into your mods/ directory, alongside the TouchController mod
- Launch and enjoy!

## What is this useful for?
In order for TouchController to work, the launcher you are using has to implement some of its functionality itself.

Not all launchers can do that for various reasons, this mod's main purpose is to target such cases.

Additionally, although unverified, it may also have slightly lower input latency than the original TouchController proxy.

## How does it work?
DirectTouch establishes a connection to the launcher using PojIntegr, 
which utilizes the similar mechanisms launchers use for GLFW.

It creates a bridge using JNI (Java Native Interface), which is a way for Java programs to communicate with native code.

DirectTouch does this on the launcher side, as well as the game side, establishing common ground in native code,
into which both sides are able to call.

## Credits
Logo concept made by FLX

Logo is an edited version of the [TouchController](https://modrinth.com/mod/touchcontroller) logo

## Licenses
This project is licensed under [LGPL-3.0](https://github.com/RaydanOMGr/DirectTouch/blob/master/LICENSE.txt)

TouchController is licensed under [LGPL-3.0](https://github.com/TouchController/TouchController/blob/master/LICENSE)

[Amethyst Android](https://github.com/AngelAuraMC/Amethyst-Android), fragments of code from which are included in the mod, is licensed under [LGPL-3.0](https://github.com/AngelAuraMC/Amethyst-Android/blob/v3_openjdk/LICENSE)

<br>
<small>This project is not affiliated with, endorsed by, or supported by TouchController or its development team.</small>