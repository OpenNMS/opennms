CFLAGS=-O2
CDEFS=-DZERO_LENGTH_PACKET=1

.c.o:
	$(CC) $(CFLAGS) $(CDEFS) -c -o $@ $<

dgspoof: dgspoof.o
	$(CC) $(CFLAGS) -o $@ dgspoof.o

clean:
	-rm -f *.o dgspoof >/dev/null 2>&1

