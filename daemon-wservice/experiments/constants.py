"""
This module contains all the constants that our experiments need
"""

GPS_LOCATIONS = {
        'Rathmines': (53.3265199,-6.2648571),
        'Santry': (53.3944773,-6.2468027),
        'Sandyford': (53.2698337,-6.2245713),
        'IKEA': (53.40741905,-6.27500737325097),
        'Monkstown': (53.2936533,-6.1539917),
        'Wadelai': (53.3867994,-6.2699323),
        'Dun Laoghaire': (53.2923448,-6.1360003),
        'Mountjoy Square': (53.3561389,-6.255421),
        'Sandymount' : (53.3318342,-6.2153462),
        'Phoenix Park' : (53.3588502,-6.33066374280722),
        'Ballycullen' : (53.27358235,-6.32541157781442),
        'Clongriffin' : (53.4026399,-6.151014),
        'Dundrum' : (53.2891457,-6.2433756),
        'Beaumont' : (53.3860585,-6.2329828),
        'Ballinteer' : (53.2764266,-6.2528361),
        'Dublin Airport' : (53.4286802,-6.25454977707029),
        'Limekiln' : (53.3043734,-6.3326528),
        'Charlestown' : (53.4039206,-6.3031688),
        'Citywest Road' : (53.2770971,-6.4148524),
        'Ringsend' : (53.3418611,-6.2267122),
        'Walkinstown Roundabout' : (53.3200169,-6.3359576),
        'East Wall Road' : (53.3551831,-6.229539),
        'Tallaght Village' : (53.2896093,-6.3595578),
        'Clare Hall Estate' : (53.3990349,-6.1625034),
        'Merrion Square' : (53.3396823,-6.24916614558252),
        'Captain\'s Hill, Leixlip' : (53.3697544,-6.4869624),
        'Baggot Street' : (53.3329104,-6.2425717),
        'Blanchardstown' : (53.3868998,-6.3775408),
        'Donnybrook' : (53.3219341,-6.2361395),
        'Castleknock' : (53.3729581,-6.3624744),
        'Stillorgan' : (53.2888378,-6.198343),
        'Heuston' : (53.34647135,-6.29405804549595)
        }

def getLocation(constant):
    return GPS_LOCATIONS(constant, False)

def getAllLocations():
    return GPS_LOCATIONS


APP_PARAMS = {
        'host_and_port' : 'http://localhost:8080/',
        'service' : 'restful-graphhopper-1.0/',
        'endpoint': 'route',
        'locale': 'en-US'
        }
def getAppParams(constant):
    return APP_PARAMS.get(constant, False)

def getAppConfig():
    return APP_PARAMS


