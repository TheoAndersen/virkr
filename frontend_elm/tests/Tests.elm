module Tests exposing (..)

import Test exposing (..)
import Expect
import Json.Decode exposing (decodeString)
import Search exposing (..)


all : Test
all =
    describe "Search json decoding"
        [ test "Can decode basic json" <|
            \_ ->
                let
                    json =
                        """ {
                              "virksomheder": [
                                {
                                  "navn": "navn",
                                  "cvrnr": "cvr",
                                  "enhedsNummer":  null,
                                  "adresseTekst": "adresse"
                                }]
                            } """
                in
                    Expect.equal (decodeString search_json_decoder json)
                        (Ok
                            { virksomheder =
                                [ { navn = "navn"
                                  , cvr = "cvr"
                                  , adresse = "adresse"
                                  }
                                ]
                            }
                        )
        ]
