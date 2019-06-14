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
    title = page.find("h1", id="PuzTitle")
    clues = page.find_all("div", "numclue")

    value = None

    puzzle_index = iter(puzzle)
    puzzle_index.__next__() #Skip dead row at start

    row_v = 0
    col_v = 0
    cells = []

    for row in puzzle_index:
        for elem in row:
            if elem != "\n":
                cell_text = str(elem.text)
                #Need to handle special chars like oo squares on themed games
                if len(cell_text) > 1:
                    cells.append(
                        {
                            "row":row_v,
                            "column":col_v,
                            "number":cell_text[:-1],
                            "letter":cell_text[-1]
                        }
                    )
                elif len(cell_text) == 1:
                    cells.append(
                        {
                            "row": row_v,
                            "column": col_v,
                            "letter": cell_text
                        }
                    )
                else:
                    cells.append(
                        {
                            "row": row_v,
                            "column": col_v,
                        }
                    )
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
                    "number":value,
                    "question":prompt[:split_index-1],
                    "answer":prompt[split_index+2:]
                }

                if index == 0:
                    across_clues.append(clue)
                else:
                    down_clues.append(clue)

    date_parts = str(title.text).split(",")
    weekday = date_parts[1].strip()

    ddate = date_parts[2].strip().split()
    month = months[ddate[0]]
    day = ddate[1]

    puzzle_date = str(month) + "/" + str(day) + "/" + date_parts[-1].strip()

    puzzle_json = {
        "board":cells,
        "across_clues":across_clues,
        "down_clues":down_clues,
        "weekday":weekday,
        "date":puzzle_date,
        "size":int(math.sqrt(cells.__len__()))
    }

    # with open(os.path.join("..", "data", "puzzle.json"), "w+") as p:
    #     json.dump(puzzle_json, p)

if __name__ == "__main__":
    if len(sys.argv) == 2:
        scrape_puzzle(sys.argv[1])
    else:
        scrape_puzzle()



