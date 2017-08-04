# soulissapp

[![gitter chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/orgs/souliss/rooms#)

You can find it published on <a href="https://play.google.com/store/apps/details?Id=it.angelic.soulissclient">Play store</a>; please note that the app itself is useless unless a souliss run-time is found on a local or remote network. Have a look at the <a href="http://souliss.github.io/welcome/">documentation</a> to get started with Souliss.

 
<img alt="Souliss IoT dashboard"   src="https://lh3.googleusercontent.com/LuBBco1CyPm7Zyz1YytKTiWGu-5s9wPzhchXkfwKmkcRM0WFQRGo8ZqZku4EgEHRPiM=h310-rw" />|<img alt="Souliss Charts"   src="https://lh3.googleusercontent.com/Zy04p_27O0f5TeXU3d1vLcVYCRqu8Od8kvX-_qNi2RmOs8wsBdAUI0o5z_JADTS1lcn0=h310-rw" />|<img alt="Souliss Charts"   src="https://lh3.googleusercontent.com/mjObldwajd1K9LHuFk6QKRRZJW69k6cHKNgshjC5scP3O1XIK0rh2dfhHC7SSo7JGXoP=h310-rw" />
 
To work on the project, just clone it from Android Studio main menu, it should compile without issues as needed libraries are encapsulated or *very* common. 

More informations are available on <a href="https://github.com/souliss/souliss/wiki/SoulissApp">wiki</a> page, feel free to contribute. If you find app errors, please just report them via android report and/or open a issue; if you believe the error is 'functional' or needs further discussion use <a href="https://github.com/souliss/soulissapp/issues">SoulissApp tracker</a> to create a new issue.

SoulissApp works on every Android devices from API 11 (HONEYCOMB) on.

## Download

SoulissApp is available for download on the Play store

<a href="https://play.google.com/store/apps/details?id=it.angelic.soulissclient">
<img alt="Get it on Google Play"   src="http://steverichey.github.io/google-play-badge-svg/img/en_get.svg" /></a>
</a>



## How to build and contribute

The easiest way to contribute is using <a href="https://developer.android.com/sdk/index.html">Android Studio</a>. Once installed, Import new project from GitHub (New -> Project from Version control -> GitHub) using the following _Git Repository URL_

    https://github.com/souliss/soulissapp.git
    
The project includes *SoulissLib* and other necessary modules. You may want to edit <a href="https://github.com/souliss/soulissapp/blob/master/soulissLib/src/main/res/values">translation Files</a> and commit them on separate branches. More info on how to use git and branching model is available on our <a href="https://github.com/souliss/souliss/wiki/Contribute">wiki</a>.

## Used Libraries

* [android-parallax-recycleview](https://github.com/kanytu/android-parallax-recyclerview)
* [AndroidCharts](https://github.com/HackPlan/AndroidCharts)
* [ColorCrossfade](https://github.com/noties/ColorCrossFade)

## Release Notes

### 2.0.5
New Dynamic Dashboard screen
Nested Tags
New Icon set

### 1.8.2
Bugfixes #122, #128, #118

### 1.8.1
RGB patterns
T6n support

### 1.8.0
new chart lib
new Tag lib used to show tag icons around the app
Slovenian by marksev1

### 1.7.0
multiple souliss networks support
Demo mode
autocomplete some options

### 1.6.1
Bugfixes #84
Broadcast Receiver for Automate integration
Tasker action&condition plugin

### 1.6.0
Added Android wear support
fixed #75, #69, #1, #80, #78, #79
Visual enhancements, synch status


### 1.5.4
send commands with voice
TAGs are now backed-up when saving DB
Fixed bugs: #1, #2, #64, #65, #66
german translation (thanks to Niels)
better tag images handling
added fahreneit conversions

### 1.5.3
Bugfixes #17
Russian and polish (thanks Damian) translations
Switches

### 1.5.2
Fixed bugs #6, #7, #9, #14, #19, #21
More powerful service life detection
new functionality: warn me if typical turned on more than x
Typical detail pane now shows favs/TAG infos

### 1.5.0
can edit programs
can use scenes in widgets
new TAG feature, create your tags and assign typicals to them
can use massive commands in programs

### 1.3.3
Material first support
Swipe to refresh

### 1.3.2
Fixes bugs #96 #98
Added Typicals T1A, T31 (finally)

### 1.3.0
Broadcast configuration to Souliss
Online status icon
Auto-configuration feature

### 1.2.3
portoguese and spanish translations

### 1.2
color-coded list items
Massive commands support (send to all nodes)
scenes bugfixes
RGB Music sync

### 1.0.0
Network layer rewritten for performance and reliability

## Privacy

Play store requires me to advise you about the fact SoulissApp is requesting RECORD_AUDIO permission. Actually, nothing is being recorded, that permission is requested only when synching lights to music. Audio has to flow thru main channel, thus requiring that permission. 

More in general, SoulissApp DOES NOT track, record or share ANY user information whatsoever. You can check it in the sources
