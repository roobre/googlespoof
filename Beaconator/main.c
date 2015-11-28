#include <stdio.h>
#include <lorcon2/lorcon.h>
#include <lorcon2/lorcon_packasm.h>

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

    if ((result = lorcon_open_injmon(context)) < 0) {
        printf("Error opening context.\n");
        return 1;
    } else {
        printf("Opening lorcon context: %d.\n", result);
    }

    uint8_t source[] = {0x00, 0x1B, 0x11, 0xD7, 0xA7, 0xFC};

    metapack = lcpa_init();
    //          pack      src    bssid   frame  dur  frag  seq  timest  beac  cap
    lcpf_beacon(metapack, "Roo", source, )


    lorcon_close(context);
    lorcon_free(context);
    return 0;
}