PROGRAM			=	validator-nu-standalone

all:			$(PROGRAM)

$(PROGRAM):		validator
				
validator:		
				-mkdir checker
				make -C checker all				
clean:			
				make -C checker clean
				rm -rf lib/*.jar
				
dist-clean:		clean
				make -C checker dist-clean