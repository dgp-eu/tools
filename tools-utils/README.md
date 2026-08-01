# ArchiveFolders


## Features implemented

* ArchiveFolders                                --archivingExecutable (0..1)
                                                    --archivePassword (0..1)
                                                        --archivePrefix (0..1)
                                                            --archiveSuffix (0..1)
                                                                --folderName (1..*) FolderNameOptionMixinClass
                                                                    --folderDestination (1) FolderDestinationOptionMixinClass