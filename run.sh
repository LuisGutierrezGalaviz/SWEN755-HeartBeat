#!/bin/sh
cd src
java Monitor&
java Main 7&
java Main 50&
read