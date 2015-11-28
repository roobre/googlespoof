#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <lorcon2/lorcon.h>
#include <lorcon2/lorcon_packasm.h>
#include <lorcon2/lorcon_forge.h>

int main(int argc, char** argv) {
    int result = 0;
    lorcon_t* context;
    lorcon_driver_t* driver;

    lcpa_metapack_t* metapack;

    printf("Using interface %s.\n", argv[1]);

    driver = lorcon_auto_driver(argv[1]);
    printf("Using driver %s.\n", driver->name);

    context = lorcon_create(argv[1], driver);
    lorcon_free_driver_list(driver);

    if ((result = lorcon_open_inject(context)) < 0) {
        printf("Error opening context.\n");
        return 1;
    } else {
        printf("Opening lorcon context: %d.\n", result);
    }

    uint8_t channel = 1;
    lorcon_set_channel(context, channel);

    int seq = 0;
    uint8_t source[] = {0x00, 0x1B, 0x11, 0xD7, 0xA7, 0xFC};
    char ssid[] = "Roobre lo peta 69";
    uint8_t rates[] = {0x82, 0x84, 0x8b, 0x96};
    uint8_t extrarates[] = {0x0c, 0x12, 0x18, 0x24, 0x30, 0x48, 0x60, 0x6c};
    uint8_t mswpa[] = {0xdd, 0x18, 0x00, 0x50, 0xf2, 0x01, 0x01, 0x00, 0x00, 0x50, 0xf2, 0x04, 0x01, 0x00, 0x00, 0x50, 0xf2, 0x04, 0x01, 0x00, 0x00, 0x50, 0xf2, 0x02, 0x00, 0x00};

    struct timespec timesleep;
    timesleep.tv_sec = 0;
    timesleep.tv_nsec = 50 * 1000 * 1000;

    uint8_t* packet = NULL;
    size_t packetsize;

    for (; ;) {

        metapack = lcpa_init();

        //          pack      src     bssid   frame   dur  frag  seq    timest                 beac  cap
        lcpf_beacon(metapack, source, source, 0x8000, 0, 0, seq++, (uint64_t) time(NULL), 100, 0x8431);
        // ssid
        lcpf_add_ie(metapack, (uint8_t) 0, strlen(ssid), ssid);
        // rates
        lcpf_add_ie(metapack, (uint8_t) 1, sizeof(rates), rates);
        // extra rates
        lcpf_add_ie(metapack, (uint8_t) 50, sizeof(extrarates), extrarates);
        // channel
        lcpf_add_ie(metapack, (uint8_t) 3, sizeof(channel), &channel);
        // MS WPA
        lcpf_add_ie(metapack, (uint8_t) 221, sizeof(channel), mswpa);

        packetsize = lcpa_size(metapack);
        if (packet == NULL)
            packet = malloc(packetsize);
        lcpa_freeze(metapack, packet);
        printf("Sending beacon %d\n", seq);
        lorcon_send_bytes(context, packetsize, packet);

        if (nanosleep(&timesleep, NULL) == -1) {
            printf("%s\n", strerror(errno));
        }

        lcpa_free(metapack);
    }

    free(packet);
    lorcon_close(context);
    lorcon_free(context);
    return 0;
}