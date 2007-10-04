About miniTruco Libraries and Tools
-----------------------------------

miniTruco is an open-source (GPL) implementation of a Brazilian card game
for mobile devices, created by me (Chester) with contributions from several
other people (see copyright notes in individual files).

This text clarifies the distribuiton of libraries from other open source
projects whithin miniTruco's source repositories (which can be found at
http://code.google.com/p/minitruco/), citing their original copyright
owners and distribution sites, in compliance with the terms of LGPL
and other library licenses under which those libraries were released.

It is recommended that any programmer intending to use those libraries
in his/her own projects download their full distributions on the mentioned
sites, and read their license agreements to make sure his use does not
violate such licenses.

I believe in good faith that this redistribution is in compliance with the
licenses of each software included, but if there is any problem in this
field, or if you have any questions/comments, feel free to contact me:

  Chester (Carlos Duarte do Nascimento)
  cd@pobox.com
  http://chester.blog.br


Sobre as Bibliotecas e Ferraemntas do miniTruco
-----------------------------------------------

Como qualquer projeto Java, o miniTruco depende de várias bibliotecas (.jar)
para ser compilado e testado.

As versões anteriores à 3 exigiam que estas fossem baixadas, descompactadas
e instaladas separadamente, o que dificultava para quem queria apenas baixar
o projeto e experimentar com ele.

O kit de desenvolvimento MIDP é um desses casos complicados. Embora ele
seja necessário para dar o build final, é possível trabalhar apenas tendo
o midp.jar e btapi.jar, que contém as classes (apenas o "esqueleto", não a
implementação) que compõem, respectivamente, MIDP 1.0 e JSR 82 (Bluetooth).

Verifiquei que as bibliotecas usadas eram (quase) todas LGPL ou de alguma
licença compatível. Então resolvi incluí-las, junto com os esqueletos
mencionados (que compilei a partir de fontes que achei na web, provavelmente
gerados a partir das especificações).

Segue uma relação dos arquivos .jar que acompanham este projeto, informando
os nomes das bibliotecas, suas URLs de download e notas de copyright, em
conformidade com as normas da LGPL e licenças equivalentes das bibliotecas.

Recomendo que qualquer projeto que vá utilizá-las faça-o com versões
atualizadas (e completas, com documentação, licença e outros itens importantes)
a partir dos sites relacionados.


Tools/Libraries List (Lista de Bibliotecas/Ferramentas)
-------------------------------------------------------

antenna-bin.jar
  Antenna
  An Ant end-to-end Solution For Wireless Java
  (c) 2002-2006 Jörg Pleumann (LGPL)
  http://antenna.sourceforge.net/

proguard.jar
  ProGuard
  A free Java class file shrinker, optimizer, and obfuscator
  Copyright (c) 2002-2006 Eric Lafortune (GPL)
  http://proguard.sourceforge.net/
  
microemulator.jar
  MicroEmulator
  J2ME Device Emulator
  Copyright (C) 2001 Bartek Teodorczyk (LGPL) 
  http://www.microemu.org

midp.jar
  MIDP for OS X
  A Darwin/OSX port of the MIDP Reference Implementation
  Created by Michael Powers, based on CLDC/MIDP reference implementation (SCSL)
  http://mpowers.net/midp-osx/
  
bt-api.jar
  Bluetooth API (JSR-82) skeleton
  Compiled version of JSR-82 sources
  Compiled by myself, based on btapi-source.zip from the final JSR-82 (which
  states (c) Copyright 2001, 2002 Motorola, Inc, but is also SCSL)
  http://jcp.org/aboutJava/communityprocess/final/jsr082/index.html

me4se.jar
  ME4SE
  J2ME Device Emulator
  (C) 2005 Stefan Haustein, Michael Kroll, Jörg Pleumann.
  http://kobjects.sourceforge.net/me4se/index.shtml

bluecove.jar
  BlueCove
  A Java library for Bluetooth (JSR-82 implementation)
  (c) 2004-2007 BlueCove Team
  http://bluecove.sourceforge.net/

Obs.: In some cases I renamed the .jar name, removing file version numbers
(so they can be upgraded without having to edit the Ant buildfile).