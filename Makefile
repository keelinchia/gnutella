# the compiler: gcc for C program, define as g++ for C++
# CC = gcc
JCC = javac
# compiler flags:
#  -g    adds debugging information to the executable file
#  -Wall turns on most, but not all, compiler warnings
# CFLAGS  = -g -lpthread #-Wall
JFLAGS = -g

# the build target executable:
default: Servent.class Servent3.class 

#Server: Server.c
#        $(#CC) $(#CFLAGS) -o Server Server.c

Servent.class: Servent.java
	$(JCC) $(JFLAGS) Servent.java

Servent3.class: Servent3.java
	$(JCC) $(JFLAGS) Servent3.java

clean:
	$(RM) Server
	$(RM) *.class
	$(RM) *~
