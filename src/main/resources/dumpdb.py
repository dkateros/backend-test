import sys
import csv
import psycopg2

try:
    connect_str = "user='" + sys.argv[1] + "' password='" + sys.argv[2] + "' host='" + sys.argv[3] + "' dbname='" + sys.argv[4] + "'"
    conn = psycopg2.connect(connect_str)
    cursor = conn.cursor()
    cursor.execute("SELECT * from dns_record")

    rows = cursor.fetchall()

    file = open(sys.argv[5], "w")
    writer = csv.writer(file)
    for row in rows :
        writer.writerow(row)
    print("wrote " + str(len(rows)) + " row to " + sys.argv[5])
except Exception as e:
    print("unexpected error")
    print(e)
finally:
    cursor.close()
    conn.close()
    file.close()

