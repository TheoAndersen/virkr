module Tests exposing (..)

import Test exposing (..)
import Expect


-- Check out http://package.elm-lang.org/packages/elm-community/elm-test/latest to learn more about testing in Elm!

import Json.Decode exposing (decodeString)
import Json.Decode.Pipeline exposing (decode, required)


json_virksomhed_decoder : Json.Decode.Decoder Virksomhed
json_virksomhed_decoder =
    decode Virksomhed
        |> required "navn" Json.Decode.string
        |> required "cvrnr" Json.Decode.string
        |> required "adresseTekst" Json.Decode.string


search_json_decoder : Json.Decode.Decoder SearchResult
search_json_decoder =
    decode SearchResult
        |> required "virksomheder" (Json.Decode.list json_virksomhed_decoder)


isOk result =
    case result of
        Ok _ ->
            True

        Err _ ->
            False


type alias Virksomhed =
    { navn : String
    , cvr : String
    , adresse : String
    }


type alias SearchResult =
    { virksomheder : List Virksomhed
    }


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
