TOPTARGETS := all help build test install uninstall clean clean-all

SUBDIRS := $(patsubst %/Makefile,%,$(wildcard */Makefile))

$(TOPTARGETS): $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
