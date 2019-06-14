#!/usr/bin/env python3.7

import requests
import math
from bs4 import BeautifulSoup, SoupStrainer

from contextlib import closing

import os
import sys
import random
import json
from datetime import timedelta, date, datetime


site = "http://xwordinfo.com/"

months = {
    "January":1,
    "February":2,
    "March":3,
    "April":4,
    "May":5,
    "June":6,
    "July":7,
    "August":8,
    "September":9,
    "October":10,
    "November":11,
    "December":12
}

def scrape_puzzle(mdy=None): #Split this into multiple functions rather than blockboi
    #Check pre-era
    extension = "Crossword?date="

    if mdy is None: #Need try-catch block to see if puzzle has come out yet, switch dt -> dt - 1 (not sure how to handle this yet)
        d = datetime.today()

        cday = str(d.day) if d.day >= 10 else str(d.day)[1:]
        cmonth = str(d.month)
        cyear = str(d.year)
        mdy = cmonth + "/" + cday + "/" + cyear
    else:
        mdy_year = int(mdy[mdy.rfind("/")+1:])
        mdy_month = int(mdy[:mdy.find("/")])
        mdy_day = int(mdy[mdy.find("/")+1:mdy.rfind("/")])

        if mdy_year < 1993 or (mdy_year == 1993 and mdy_month < 11): #Not full check, don't have net to check the exact date
            extension = "PS?date="
        else:
            pass

    #Also need check if pre-1942 to handle non-existent puzzles!

    page = BeautifulSoup(requests.get(site + extension + mdy).text, "html.parser")


    puzzle = page.find("table", id="PuzTable")

    header = page.find("h1", id="PuzTitle")
    subtitle = page.find("h2", id="CPHContent_SubTitle")

    clues = page.find_all("div", "numclue")

    value = None

    puzzle_index = iter(puzzle)
    puzzle_index.__next__() #Skip dead row at start

    row_v = 0
    col_v = 0
    cells = []

    for row in puzzle_index:
        col_v = 0
        for elem in row:
            if elem != "\n":
                cell_dict = {
                    "row":row_v,
                    "column":col_v
                }

                special_value = elem.findChildren("div", {'class', 'subst'})
                value = elem.findChildren("div", {'class', 'letter'})
                marker = elem.findChildren("div", {'class', 'num'})

                if len(marker) != 0:
                    if len(marker[0].text) > 0:
                        cell_dict["marker"] = marker[0].text

                if len(special_value) != 0:
                    cell_dict["value"] = special_value[0].text
                    cell_dict["rebus"] = True
                elif len(value) != 0:
                    cell_dict["value"] = value[0].text
                else:
                    cell_dict["wall"] = True

                if elem.get('class') is not None:
                    if 'bigcircle' in elem.get('class'):
                        cell_dict["circled"] = True
                    if 'bigshade' in elem.get('class'):
                        cell_dict['shaded'] = True

                cells.append(cell_dict)

                col_v += 1
        row_v += 1
        # print("END ROW")
    print() #Crosswords always square, sqrt this should be row/col count

    across_clues = []
    down_clues = []

    for index in range(2):
        for across_clue in clues[index]:
            try:
                value = int(across_clue.text) #Hackish way of doing mod 2 check, since value div -> q + a div is structure of html
            except ValueError:
                prompt = str(across_clue.text)
                split_index = prompt.rfind(":") #Formatting on site is that a colon delimits q from a

                clue = {
                    "marker":value,
                    "question":prompt[:split_index-1],
                    "answer":prompt[split_index+2:]
                }

                if index == 0:
                    across_clues.append(clue)
                else:
                    down_clues.append(clue)

    if subtitle is None:
        date_parts = str(header.text).split(",")
    else:
        date_parts = str(subtitle.text).split(",")

    weekday = date_parts[1].strip()

    ddate = date_parts[2].strip().split()
    month = months[ddate[0]]
    day = ddate[1]

    puzzle_date = str(month) + "/" + str(day) + "/" + date_parts[-1].strip()

    puzzle_json = {
        "board":cells,
        "size":int(math.sqrt(cells.__len__())),
        "across_clues":across_clues,
        "down_clues":down_clues,
        "weekday":weekday,
        "date":puzzle_date,
    }

    if subtitle is not None:
        puzzle_json["title"] = header.text

    with open(os.path.join(os.path.dirname(__file__), "..", "data", "puzzle.json"), "w+") as p:
        json.dump(puzzle_json, p)

if __name__ == "__main__":
    if len(sys.argv) == 2:
        scrape_puzzle(str(sys.argv[1]))
    else:
        scrape_puzzle()



