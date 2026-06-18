# JsonSplit
logic to split large JSON files with array content into smaller files to digest easily by various ETL tools


## Features implemented

* JsonSplit                                     --inFileName (1..*) InFileNameOptionMixinClass
                                                    --folderDestination (1) FolderDestinationOptionMixinClass
                                                        --splitSize (0..1)
                                                            --field (1)
                                                                --bucketLength (0..1)
