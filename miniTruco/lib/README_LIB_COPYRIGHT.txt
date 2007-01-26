About miniTruco Libraries and Tools
-----------------------------------

miniTruco is an open-source (GPL) implementation of a Brazilian card game
for mobile devices, created by me (Chester) with contributions from several
other people.

This file clarifies the distribuiton of libraries from other open source
projects whithin miniTruco's source repositories (which can be found at
http://code.google.com/p/minitruco/), citing their original copyright
owners and distribution sites, in compliance with the terms of LGPL.

It is written in portuguese (as is most of miniTruco's documentation, since
it is a game of little interest for non-Brazilians), but the library list
at the end of the document should be clear for anyone.

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

Infelizmente, o Converter.jar (usado para gerar a versão .prc, compatível
com JVMs de Palm mais antigas) não possui uma licença compatível com esse
tipo de distribuição. Aliando isso às dificuldades questas JVMs já têm,
optei por descontinuar o suporte a Palm.

Segue uma relação dos arquivos .jar que acompanham este projeto, informando
os nomes das bibliotecas, suas URLs de download e notas de copyright, em
conformidade com as normas da LGPL.

Recomendo que qualquer projeto que vá utilizá-las faça-o com versões
atualizadas (e completas, com documentação, licença e outros itens importantes)
a partir dos sites relacionados.


Tools/Libraries List (Lista de Bibliotecas/Ferramentas)
-------------------------------------------------------

antenna-bin.jar
  Antenna
  An Ant-to-End Solution For Wireless Java
  (c) 2002-2006 Jörg Pleumann (LGPL)
  http://antenna.sourceforge.net/

proguard.jar
  ProGuard
  A free Java class file shrinker, optimizer, and obfuscator
  Copyright (c) 2002-2006 Eric Lafortune (GPL)
  http://proguard.sourceforge.net/
  
me-app.jar
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

Obs.: In some cases I renamed the .jar name, removing file version numbers
(so they can be upgraded without having to edit the Ant buildfile).