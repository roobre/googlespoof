<?php

define('WIGLEURL', 'https://wigle.net/api/v1/jsonSearch');

//define('AUTHCOOKIE', 'pichu:312993165:1431204925:5pc0UTY8nQDxOvrEn6V3gg');
define('AUTHCOOKIE', 'cybercamp2:282401487:1448674819:GITL4702g/iluzMM8TdXbw');

define('MDK3_MONITOR', 'wlp0s20u3mon');
define('AIRBASE_MONITOR', 'wlan0mon');
define('MONTHS', '12');
define('LAT', '41.3879');
define('LNG', '2.1698');

define('OFFSET', '0.0003');
define('NUMAPS', '40');

date_default_timezone_set('Europe/Madrid');

if (in_array('local', $argv)) {
    print "Using local data.json.\n\n";
    $data = json_decode(file_get_contents(__DIR__ . '/data.json'), 1);
} else {
    print "Fetching networks from Wigle...\n\n";
    $ch = curl_init(WIGLEURL);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 'latrange1=' . (LAT)  . '&latrange2=' . (LAT + OFFSET) . '&longrange1=' . (LNG) . '&longrange2=' . (LNG - 3*OFFSET));
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
    print "Dumping json.\n\n";
    file_put_contents(__DIR__ . '/data.json', json_encode($data, JSON_PRETTY_PRINT));
}

print "Sorting by proximity...\n\n";
usort($data['results'], function ($net1, $net2) {
    $theta = $net1['trilong'] - $net2['trilong'];
    $dist = sin(deg2rad($net1['trilat'])) * sin(deg2rad($net2['trilat'])) +  cos(deg2rad($net1['trilat'])) * cos(deg2rad($net2['trilat'])) * cos(deg2rad($theta));
    $dist = acos($dist);
    $dist = rad2deg($dist);

    return ($dist > 0 ? -1 : 1);
});
//print_r($data);

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
print "Killing previous mdk3 instances...\n";
exec("killall mdk3");

if (in_array('both', $argv) || in_array('airbase', $argv)) {
    print "\nCreating " . count($filter) . " networks with airbase...\n";
    foreach ($filter as $k => $network) {
        print "Creating network #$k {$network['ssid']}.\n";
        exec("airbase-ng " . (($network['wep'] == 'W') ? '-z 2' : (($network['wep'] == '2') ? '-Z 2' : (($network['wep'] == 'Y') ? '-W1' : ''))) . " --essid ".escapeshellarg($network['ssid']). " -a {$network['netid']} " . AIRBASE_MONITOR . " > /dev/null &");
    }
}

if (in_array('both', $argv) || in_array('mdk3', $argv)) {
    print "\nCreating " . count($filter) . " networks with mdk3...\n";
    $file = "";
    foreach ($filter as $k => $network) {
        $file .= $network['netid'] . " " . $network['ssid'] . "\n";
    }
    print $file . "\n\n";
    file_put_contents(__DIR__ . '/aps.lst', $file);
    exec("mdk3 " . MDK3_MONITOR . " b -s 500 -v " . __DIR__ . "/aps.lst  > /dev/null &");
}

if (!in_array('both', $argv) && !in_array('airbase', $argv) && !in_array('mdk3', $argv)) {
    print "Nothing to do.\n";
}

//file_put_contents('aps.lst', $data);
?>
