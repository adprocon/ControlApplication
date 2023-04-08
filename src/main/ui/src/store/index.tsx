import {configureStore} from "@reduxjs/toolkit";
import {tagListSlice} from "../reducers";

export const store = configureStore({
    reducer: {
        tagList: tagListSlice.reducer,
    }
})