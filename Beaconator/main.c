#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "beaconator.h"
#include "parser.h"

int main(int argc, char** argv) {
    /*
    ap_t* list = malloc(sizeof(ap_t));
    list->bssid[0] = 0x00;
    list->bssid[1] = 0x1B;
    list->bssid[2] = 0x11;
    list->bssid[3] = 0xD7;
    list->bssid[4] = 0xA7;
    list->bssid[5] = 0xFC;
    strcpy(list->essid, "Roobre lo peta 69");

    list->next = malloc(sizeof(ap_t));
    list->next->bssid[0] = 0x00;
    list->next->bssid[1] = 0x1B;
    list->next->bssid[2] = 0x11;
    list->next->bssid[3] = 0xD7;
    list->next->bssid[4] = 0xA7;
    list->next->bssid[5] = 0xFC;
    strcpy(list->next->essid, "Roobre lo peta 24");
    list->next->next = NULL;
     */

    if (argc < 3) {
        fprintf(stderr, "Usage: %s <interface-monitor> <path/to/aps.lst>\n", argv[0]);
        exit(1);
    }

    ap_t* list;

    parse_aps(argv[2], &list);
    send_beacons(argv[1], list);
}