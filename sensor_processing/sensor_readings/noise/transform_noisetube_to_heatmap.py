from collections import defaultdict
import pickle
import jsonpickle

def expo_moving_average(data):
    """
    Calculates the exponential moving average for every unique lat,lon 
    combination. Assumes data is in the format:
        [ [lat1, lon1, val1],
          [lat2, lon2, val2],
          [lat1, lon1, val3]
          [lat2, lon2, val4],
          [lat2, lon2, val5],
          ...
          ]
    First, we create a dict containing each unique lat-lon combination as the 
    key, and list of all loudness values observed, as the value
    Second, for each key, we calculate the exponential moving average for the 
    list stored in value.

    Returns a dict of the form
    { (lat1,lon1): ema1,
      (lat2,lon2): ema2
      }
    ema is calculated using the formula:
        ema = new_valu + ALPHA * (old_val - new_val)
    """
    ALPHA = 0.25
    ret_hash = defaultdict(int)
    collected_hash = defaultdict(list)

    for datum in data:
        key = tuple([datum[0],datum[1]])
        value = datum[2]
        collected_hash[key].append(value)

    for key,val in collected_hash.iteritems():
        ret_hash[key] = reduce(lambda x,y: y + (ALPHA * (x - y)), val, val[0])

    return ret_hash

def normalize(data):
    """
    Assumes data is a dict of the form:
    { (lat1,lon1): ema1,
      (lat2,lon2): ema2
      }

    Returns a normalized list of the form:
    [ [lat1,lon1, 0.3],
      [lat2, lon2, 0.5],
      ...
      ]

    Normalization is done by the following process:
        1. Collect all ema values
        2. Find max_ema, min_ema
        for each key, val in data:
            data[key] =  normalized-ema = (ema - min_ema)/ (max_ema - min_ema)

    """
    collected_ema = []
    for key,val in data.iteritems():
        collected_ema.append(val)

    min_ema = min(collected_ema)
    max_ema = max(collected_ema)
    ema_diff = max_ema - min_ema

    for key, val in data.iteritems():
        data[key] = (val - min_ema)/ema_diff

    heatmap_list = []
    for key,val in data.iteritems():
        locationval = list(key)
        locationval.append(val)
        heatmap_list.append(locationval)

    return heatmap_list

def convert_and_write(noisetube_file, heatmap_file):
   """
   Converts the data into format required by heatmap library and writes
   into file given in heatmap_file.

   Assumes the noisetube_file is in JSON format. JSON allows null as a valid 
   identifier, but we don't care about malformed readings with null. So, we 
   ignore any lat/lon that is null.
   """
   cleaned_data = []
   readings_from_json = open(noisetube_file, 'r').read()
   print("Finished reading json data")
   readings_data = jsonpickle.decode(readings_from_json)
   for reading in readings_data:
       if reading['lat'] is None or reading['lng'] is None:
           continue
       else:
           datum = [reading['lat'], reading['lng'], reading['loudness']]
           cleaned_data.append(datum)
   print("Number of items post-cleaning: %d"%(len(cleaned_data)))
   ema_data = expo_moving_average (cleaned_data)
   heatmap_data = normalize(ema_data)
   with open(heatmap_file, 'w') as hf:
    hf.write('[')
    hf.write(",".join([str(x) for x in heatmap_data]))
    hf.write(']')
    print("Finished writing heatmap data")

if __name__ == '__main__':
    convert_and_write('latest_noisetube_readings.json', 'heatmap_file.dat')


