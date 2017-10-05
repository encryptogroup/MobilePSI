# MobilePSI

### Implementation of precomputed PSI for unequal set sizes for smartphone

By *Ágnes Kiss, Thomas Schneider* ([ENCRYPTO](http://www.encrypto.de), TU Darmstadt),  *Jian Liu, N. Asokan* (Aalto University), and *Benny Pinkas* ([Bar Ilan University](http://www.pinkas.net/)) <br>in [PoPETs 2017](https://petsymposium.org/2017/). Paper available [here](http://encrypto.de/papers/KLSAP17.pdf).

Our GC-PSI implementation uses [FlexSC](https://github.com/wangxiao1254/FlexSC) or [ObliVMGC](https://github.com/oblivm/ObliVMGC) as a garbled circuit backend. 

### Protocols
---

Our PSI implementation implements four PSI protocols in the precomputation setting in Java that can be run on Android smartphones. The four protocols are
* <b>RSA-PSI</b>: The RSA Blind Signature based PSI protocol
* <b>DH-PSI</b>: The Diffie-Hellman-based PSI protocol
* <b>NR-PSI</b>: Naor-Reingold PRF-based protocol
* <b>GC-PSI</b>: The AES GC-based PSI protocol

### Requirements
---

### Sourcecode
---

#### File System Structure

* `/PSIClient/` - Client implementation in Java to be run on a smartphone
* `/PSIServer/` - Server implementation in Java to be run on a PC

#### PSI implementation

1. Clone a copy of the main MobilePSI git repository and its submodules by running:
	```
	git clone --recursive git://github.com/encryptogroup/MobilePSI
	```

2. 
