#!/usr/bin/env python3.7

import requests
from contextlib import closing
from bs4 import BeautifulSoup, SoupStrainer

import os
import sys
import random
import json

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

def scrape_puzzle(mdy=None):
    #Check pre-era
    page = BeautifulSoup(requests.get(site + "Crossword?date=6/13/2019").text, "html.parser")

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
        print("END ROW")
    print(cells.__len__()) #Crosswords always square, sqrt this should be row/col count

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

    date = date_parts[2].strip().split()
    month = months[date[0]]
    day = date[1]

    puzzle_date = str(month) + "/" + str(day) + "/" + date_parts[-1].strip()
    print(puzzle_date)

    puzzle_json = {
        "board":cells,
        "across_clues":across_clues,
        "down_clues":down_clues,
        "weekday":weekday,
        "date":puzzle_date
    }

    print(puzzle_json)

if __name__ == "__main__":
    scrape_puzzle()
    pass


