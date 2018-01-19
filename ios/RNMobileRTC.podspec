
Pod::Spec.new do |s|
  s.name         = "RNMobileRTC"
  s.version      = "0.0.1"
  s.summary      = "RNMobileRTC"
  s.description  = ""
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "Ivan Vukovic" => "3.14wee@gmail.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/reactor/react-native-mobile-rtc.git", :tag => "master" }
  s.source_files  = "RNMobileRTC/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"

end

  