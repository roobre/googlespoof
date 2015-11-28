<?php

define('WIGLEURL', 'https://wigle.net/api/v1/jsonSearch');

//define('AUTHCOOKIE', 'pichu:312993165:1431204925:5pc0UTY8nQDxOvrEn6V3gg');
define('AUTHCOOKIE', 'cybercamp2:282401487:1448674819:GITL4702g/iluzMM8TdXbw');

define('MONTHS', '18');
define('LAT', '47.6661');
define('LNG', '-122.3008');
define('OFFSET', '0.0002');
define('NUMAPS', '30');

date_default_timezone_set('Europe/Madrid');

if (in_array('local', $argv)) {
    print "Using local data.json.\n";
    $data = json_decode(file_get_contents(__DIR__ . '/data.json'));
} else {
    print "Fetching networks from Wigle...\n";
    $ch = curl_init(WIGLEURL);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 'latrange1=' . (LAT)  . '&latrange2=' . (LAT + OFFSET) . '&longrange1=' . (LNG) . '&longrange2=' . (LNG + OFFSET));
    curl_setopt($ch, CURLOPT_COOKIE, 'auth=' . AUTHCOOKIE);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $data = json_decode(curl_exec($ch), 1);

    curl_close($ch);
}



if (isset($data['results'])) {
    foreach ($data['results'] as &$result) {
        ksort($result);
    }
} else {
    print_r($data);
    exit(1);
}

if (in_array('dump', $argv)) {
    print "Dumping json.\n";
    file_put_contents(__DIR__ . '/data.json', json_encode($data, JSON_PRETTY_PRINT));
}


$filter = array();
foreach ($data['results'] as $result) {
    if ($result['wep'] != '?' && (time() - strtotime($result['lasttime']) < 60*60*24*30*MONTHS) && count($result['ssid'])) {
        $filter[] = $result;

        if (count($filter) >= NUMAPS)
            break;
    }
}

print "Killing previous airbase instances...\n";
exec("killall airbase-ng");

foreach ($filter as $k => $network) {
    print "Creating network #$k {$network['ssid']}.\n";
    exec("airbase-ng " . (($network['wep'] == 'W') ? '-z 2' : (($network['wep'] == '2') ? '-Z 2' : (($network['wep'] == 'Y') ? '-W1' : ''))) . " --essid ".escapeshellarg($network['ssid']). " -a {$network['netid']} wlp0s20u3mon > /dev/null &");
}

//file_put_contents('aps.lst', $data);
?>
