module Search exposing (..)

import Html exposing (Html, text, ul, li, i, span, a, p, section, div, h1, h2, img)
import Html.Attributes exposing (src, id, class, type_, placeholder)
import Json.Decode exposing (decodeString)
import Json.Decode.Pipeline exposing (decode, required)


-- Json Decoding


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



---- MODEL ----


type alias Model =
    { searchResult : SearchResult
    }


type alias Virksomhed =
    { navn : String
    , cvr : String
    , adresse : String
    }


type alias SearchResult =
    { virksomheder : List Virksomhed
    }


init : ( Model, Cmd Msg )
init =
    ( { searchResult =
            { virksomheder =
                [ { navn = "Nine A/S"
                  , adresse = "Kongens Nytorv 18, 1050 København K"
                  , cvr = "30714024"
                  }
                , { navn = "Number Nine ApS"
                  , adresse = "Løjt Skolegade 18, 6200 Aabenraa"
                  , cvr = "38537458"
                  }
                , { navn = "NINE EYEWEAR ApS"
                  , adresse = "Rudolfgårdsvej 1, 8260 Viby J"
                  , cvr = "33262736"
                  }
                ]
            }
      }
    , Cmd.none
    )



---- UPDATE ----


type Msg
    = NoOp


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    ( model, Cmd.none )



---- VIEW ----


searchField model =
    div
        [ id "search"
        , class "field is-grouped-centered"
        ]
        [ p [ class "control has-icons-right" ]
            [ Html.input
                [ class "input is-medium"
                , type_ "text"
                , placeholder "cvr eller navn"
                ]
                []
            , span [ class "icon is-medium is-right" ]
                [ i [ class "fas fa-search" ] [] ]
            ]
        ]


resultItem : Virksomhed -> Html Msg
resultItem virksomhed =
    Html.table [ class "table is-striped is-hoverable is-fullwidth" ]
        [ Html.tr []
            [ Html.td []
                [ text virksomhed.navn
                , Html.br [] []
                , div [ class "cvr" ] [ text virksomhed.cvr ]
                ]
            , Html.td [ class "adresse" ] [ text virksomhed.adresse ]
            ]
        ]



-- div [ class "box level" ]
--     [ div [ class "level-left" ]
--         [ div
--             [ class "level-item" ]
--             [ h2 [] [ text virksomhed.navn ]
--             , Html.br [] []
--             , p []
--                 [ text "123232"
--                 ]
--             ]
--         ]
--     , div [ class "level-right grey" ]
--         [ p []
--             [ text virksomhed.adresse
--             ]
--         ]
--     ]


searchResults : Model -> Html Msg
searchResults model =
    div []
        (List.map resultItem model.searchResult.virksomheder)


view : Model -> Html Msg
view model =
    section [ class "section is-large" ]
        [ div [ class "container" ]
            [ h1 [ class "title" ] [ text "Virkr" ]
            , p [ class "subtitle" ] [ text "søg rundt i virksomheder" ]
            , searchField model
            , searchResults model
            ]
        ]
