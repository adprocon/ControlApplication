import {createSlice} from "@reduxjs/toolkit";

const tagInit = {
    tagList: [],
    tagListLoadingStatus: 'idle',
    filter: ''
}

export const tagListSlice = createSlice({
        name: 'taglist',
        initialState: tagInit,
        reducers: {
            tagListFetching: (state) => {
                state.tagListLoadingStatus = 'loading';
            },
            tagDeleting: (state) => {
                state.tagListLoadingStatus = "deleting";
            },
            tagAdding: (state) => {
                state.tagListLoadingStatus = "adding";
            },
            tagUpdating: (state) => {
                state.tagListLoadingStatus = "updating";
            },
            tagListFethed: (state, action) => {
                state.tagListLoadingStatus = 'idle';
                state.tagList = action.payload;
            },
            tagListFetchingError: (state) => {
                state.tagListLoadingStatus = 'error'
            }
        }
    }
)
