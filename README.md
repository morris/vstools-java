__Replaced with https://github.com/morris/vstools__

This repo is dead. It has been ported to JavaScript/WebGL as a replacement.

----

# Vagrant Story Tools

This is an incomplete toolset for Vagrant Story (2000, Square). It
contains a partially working model and map viewer. It is written in Java
mainly because of higher development speed and easy deployment.
Everything is work in progress.

## Requirements

The viewer uses jMonkey3 as 3D engine. You will, of course, also need a
copy of Vagrant Story in order to view its files. The viewer opens
individual files obtained from a CD image, which have to be extracted
first.

This code is developed for the US Version of Vagrant Story.

## Install

The following guide outlines how to run the viewer from Eclipse.

1. If you haven't already, download and install Eclipse (for Java).
2. Download the vstools source and extract it in your workspace.
3. Create an Eclipse Java Project from the source.
4. Download and extract the latest nightly build of jMonkeyEngine3 from http://www.jmonkeyengine.com/nightly/ (works with jME3_2013-05-07.zip).
5. Add jMonkeyEngine3.jar to your project's build path.
6. Copy config.dist.ini to config.ini and set dataPath.
7. Run the GUI (or any other main methods, such as tests).

See also http://jmonkeyengine.org/wiki/doku.php/jme3:setting_up_jme3_in_eclipse

## Acknowledgements

Thanks to Valendian and other hackers' tremendous work on analyzing VS.

Most information on VS hacking can be found here:
http://datacrystal.romhacking.net/wiki/Vagrant_Story
