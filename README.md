About Googlespoof
=================

Googlespoof is a Proof-of-concept aimed to exploit Android's geolocation APIs which use 802.11 networks.

This POC is divided in two parts:

1. **Beaconator**: A program written in C that, given some parameters (GPS coords, accuracy, and an interface in
monitor mode), asks Wigle's API for networks on this location, parses JSON, filters out recent networks, and continously
inject beacons in order to simulate the fetched networks. This casuses Android phones which rely on WiFi networks to
provide their location they are on the point supplied to the program.

2. **SecureLoc**: An Android app which listens for network-based location requests, and compares the result to the
position reported by GPS. If they differ in a great amount, alerts the users informing his location is being spoofed.
