#!/bin/bash

rm -rf ~/.gnupg
install -d -m 700 ~/.gnupg
echo use-agent >> ~/.gnupg/gpg.conf
echo pinentry-mode loopback >> ~/.gnupg/gpg.conf
echo allow-loopback-pinentry >> ~/.gnupg/gpg-agent.conf
echo RELOADAGENT | gpg-connect-agent
echo "$GPG_SECRET_KEY" | base64 --decode | gpg --import --no-tty --batch --yes
echo "$GPG_PASSPHRASE" | base64 --decode > "$HOME/.gpg-passphrase"

cat <<END >~/.rpmmacros
%_topdir $HOME/rpmbuild
%_gpg_name 79564AEB7CC6C01488E7C64757801F6F5B9EFD43
%_source_filedigest_algorithm 0
%_binary_filedigest_algorithm 0
%_source_payload w0.bzdio
%_binary_payload w0.bzdio
#%__gpg /usr/bin/gpg2
%__gpg_sign_cmd %{__gpg} gpg --batch --no-verbose --force-v3-sigs --no-armor --no-secmem-warning --batch --pinentry-mode loopback --passphrase-file $HOME/.gpg-passphrase -u "%{_gpg_name}" -sbo %{__signature_filename} %{__plaintext_filename}
END

