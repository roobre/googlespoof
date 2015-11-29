#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>

#include "beaconator.h"
#include "wigle.h"
#include "json.h"
#include "fileutil.h"

const double DEFAULT_RANGE = 0.0003;
const int DEFAULT_MONTHS = 12;
const int DEFAULT_LIMIT = 40;

const uint8_t REQUIRED_OPTS = (0x01 | 0x02 | 0x04);

int main(int argc, char** argv) {
    char interface[16];
    char json_path[64];
    double lat, lng, range;
    int months;
    int limit;

    int opt;
    uint8_t options = 0;

    while ((opt = getopt(argc, argv, "i:a:o:r:m:l:j:")) != -1) {
        switch (opt) {
            case 'i':
                strcpy(interface, optarg);
                options |= 0x01;
                break;
            case 'a':
                lat = strtod(optarg, NULL);
                options |= 0x02;
                break;
            case 'o':
                lng = strtod(optarg, NULL);
                options |= 0x04;
                break;
            case 'r':
                range = strtod(optarg, NULL);
                options |= 0x08;
                break;
            case 'm':
                months = (int) strtol(optarg, NULL, 10);
                options |= 0x10;
                break;
            case 'l':
                limit = (int) strtol(optarg, NULL, 10);
                options |= 0x20;
                break;
            case 'j':
                strcpy(json_path, optarg);
                options |= 0x40;
                break;
        }
    }

    if (!(options & 0x08)) {
        range = DEFAULT_RANGE;
    }
    if (!(options & 0x10)) {
        months = DEFAULT_MONTHS;
    }
    if (!(options & 0x20)) {
        limit = DEFAULT_LIMIT;
    }

    if ((options & REQUIRED_OPTS) != REQUIRED_OPTS) {
        fprintf(stderr, "Usage: %s\n"
                        "\t-i <interface in monitor mode>\n"
                        "\t-a <latitude>\n"
                        "\t-o <longitude>\n"
                        "\t-r [range (%lf)]\n"
                        "\t-m [max age in months (%d)]\n"
                        "\t-l [limit max aps (%d)]\n"
                        "\t-j [local json. +file.json to dump, -file.json to load]\n", argv[0], DEFAULT_RANGE,
                DEFAULT_MONTHS,
                DEFAULT_LIMIT);
        exit(1);
    }

    ap_t* list;
    char* json;

    if (options & 0x40 && *json_path == '-') {
        printf("Loading json from file '%s'.\n", json_path + 1);
        load_str(&json, json_path + 1);
    } else {
        fetch_wigle(&json, lat, lng, range);
        if (options & 0x40 && *json_path == '+') {
            printf("Saving json to file '%s'.\n", json_path + 1);
            save_str(json, json_path + 1);
        }
    }
    parse_json(&list, json, months, limit);
    send_beacons(list, interface);
}