//
// Created by roobre on 11/28/15.
//

#include "beaconator.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <stdint.h>
#include <lorcon2/lorcon.h>
#include <lorcon2/lorcon_packasm.h>
#include <lorcon2/lorcon_forge.h>

const int DELAY = 100;

void send_beacons(const char* interface, ap_t* list) {
    int result = 0;
    lorcon_t* context;
    lorcon_driver_t* driver;

    lcpa_metapack_t* metapack;

    printf("Using interface %s.\n", interface);

    driver = lorcon_auto_driver(interface);
    printf("Using driver %s.\n", driver->name);

    context = lorcon_create(interface, driver);
    lorcon_free_driver_list(driver);

    if ((result = lorcon_open_inject(context)) < 0) {
        printf("Error opening context.\n");
        return;
    } else {
        printf("Opening lorcon context: %d.\n", result);
    }

    uint8_t channel = 1;
    lorcon_set_channel(context, channel);

    int seq = 0;
    uint8_t rates[] = {0x82, 0x84, 0x8b, 0x96};
    uint8_t extrarates[] = {0x0c, 0x12, 0x18, 0x24, 0x30, 0x48, 0x60, 0x6c};
    uint8_t mswpa[] = {0xdd, 0x18, 0x00, 0x50, 0xf2, 0x01, 0x01, 0x00, 0x00, 0x50, 0xf2, 0x04, 0x01, 0x00, 0x00, 0x50,
                       0xf2, 0x04, 0x01, 0x00, 0x00, 0x50, 0xf2, 0x02, 0x00, 0x00};

    struct timespec timesleep;
    timesleep.tv_sec = 0;
    timesleep.tv_nsec = DELAY * 1000 * 1000;

    uint8_t* packet = NULL;
    size_t packetsize;

    ap_t* current;

    for (; ;) {
        current = list;
        while (current != NULL) {
            metapack = lcpa_init();
            //          pack      src     bssid   frame   dur  frag  seq    timest                 beac  cap
            lcpf_beacon(metapack, current->bssid, current->bssid, 0x8000, 0, 0, seq++, (uint64_t) time(NULL), 100, 0x8431);
            // ssid
            lcpf_add_ie(metapack, (uint8_t) 0, strlen(current->essid), current->essid);
            // rates
            lcpf_add_ie(metapack, (uint8_t) 1, sizeof(rates), rates);
            // extra rates
            lcpf_add_ie(metapack, (uint8_t) 50, sizeof(extrarates), extrarates);
            // channel
            lcpf_add_ie(metapack, (uint8_t) 3, sizeof(channel), &channel);
            // MS WPA
            lcpf_add_ie(metapack, (uint8_t) 221, sizeof(channel), mswpa);

            packetsize = lcpa_size(metapack);
            packet = malloc(packetsize);
            lcpa_freeze(metapack, packet);
            printf("Sending beacon #%d for %s\n", seq, current->essid);
            lorcon_send_bytes(context, packetsize, packet);

            lcpa_free(metapack);
            free(packet);

            current = current->next;
        }

        nanosleep(&timesleep, NULL);
    }

    lorcon_close(context);
    lorcon_free(context);
}